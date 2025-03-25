import aiohttp
from datetime import datetime, timedelta

class WeatherData:
    def __init__(self, service: str, time: list[str], temp_max: list[float], temp_min: list[float], weather_code: list[int]):
        self.type = "plot"
        self.service = service
        self.time = time
        self.temp_max = temp_max
        self.temp_min = temp_min
        self.weather_code = weather_code

open_meteo_weather_code = {
    0: 0, # Clear sky
    1: 1, # Mainly clear
    2: 2, # partly cloudy
    3: 3, # overcast
    45: 4, # fog
    48: 4, # depositing rime fog
    51: 5, # Drizzle light
    53: 5, # Drizzle moderate
    55: 5, # Drizzle dense
    56: 5, # Freezing drizzle light
    57: 5, # Freezing drizzle dense
    61: 6, # Rain light
    63: 6, # Rain moderate
    65: 6, # Rain dense
    66: 6, # Freezing rain light
    67: 6, # Freezing rain dense
    71: 7, # Snow light
    73: 7, # Snow moderate
    75: 7, # Snow dense
    77: 7, # Snow grains
    80: 6, # Rain showers slight
    81: 6, # Rain showers moderate
    82: 6, # Rain showers heavy
    85: 7, # Snow showers slight
    86: 7, # Snow showers heavy
    95: 8, # Thunderstorm
    96: 8, # Thunderstorm with slight hail
    99: 8, # Thunderstorm with heavy hail
}

OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast"

async def get_open_meteo_weather(lat, lng, past_days, forecast_days):
    params = {
        "latitude": lat,
        "longitude": lng,
        "timezone": "auto",
        "past_days": past_days,
        "forecast_days": forecast_days,
        "daily": "temperature_2m_max,temperature_2m_min,weather_code"
    }
    async with aiohttp.ClientSession() as session:
        async with session.get(OPEN_METEO_URL, params=params) as response:
            status_code = response.status
            response_json = await response.json()
    if status_code != 200: return status_code, response_json
    else: return 200, WeatherData(
        service="OpenMeteo",
        time=response_json["daily"]["time"],
        temp_max=response_json["daily"]["temperature_2m_max"],
        temp_min=response_json["daily"]["temperature_2m_min"],
        weather_code=[open_meteo_weather_code[x] for x in response_json["daily"]["weather_code"]]
    ).__dict__

weatherbit_weather_code = {
    200: 8,  # Thunderstorm with light rain
    201: 8,  # Thunderstorm with rain
    202: 8,  # Thunderstorm with heavy rain
    230: 8,  # Thunderstorm with light drizzle
    231: 8,  # Thunderstorm with drizzle
    232: 8,  # Thunderstorm with heavy drizzle
    233: 8,  # Thunderstorm with Hail
    300: 5,  # Light Drizzle
    301: 5,  # Drizzle
    302: 5,  # Heavy Drizzle
    500: 6,  # Light Rain
    501: 6,  # Moderate Rain
    502: 6,  # Heavy Rain
    511: 6,  # Freezing rain
    520: 6,  # Light shower rain
    521: 6,  # Shower rain
    522: 6,  # Heavy shower rain
    600: 7,  # Light snow
    601: 7,  # Snow
    602: 7,  # Heavy Snow
    610: 7,  # Mix snow/rain
    611: 7,  # Sleet
    612: 7,  # Heavy sleet
    621: 7,  # Snow shower
    622: 7,  # Heavy snow shower
    623: 7,  # Flurries
    700: 4,  # Mist
    711: 4,  # Smoke
    721: 4,  # Haze
    731: 4,  # Sand/dust
    741: 4,  # Fog
    751: 4,  # Freezing Fog
    800: 0,  # Clear sky
    801: 1,  # Few clouds
    802: 2,  # Scattered clouds
    803: 2,  # Broken clouds
    804: 3,  # Overcast clouds
    900: 5,  # Unknown Precipitation
}

WEATHERBIT_URL = "https://api.weatherbit.io/v2.0/forecast/daily"
WEATHERBIT_PAST_URL="https://api.weatherbit.io/v2.0/history/daily"

async def get_weatherbit_weather(lat, lng, past_days, forecast_days, key):

    weather_data = None
    async with aiohttp.ClientSession() as session:
        if past_days != 0:
            params = {
                "lat": lat, "lon": lng,
                "start_date": (datetime.today() - timedelta(days=past_days)).strftime('%Y-%m-%d'),
                "end_date": datetime.today().strftime('%Y-%m-%d'),
                "key": key
            }
            async with session.get(WEATHERBIT_PAST_URL, params=params) as response:
                status_code = response.status
                response_json = await response.json()
            if status_code != 200: return status_code, response_json
            weather_data = WeatherData(
                service="WeatherBit",
                time=[row["datetime"] for row in response_json["data"]],
                temp_max=[row["max_temp"] for row in response_json["data"]],
                temp_min=[row["min_temp"] for row in response_json["data"]],
                weather_code=[-1] * len(response_json["data"])
            ).__dict__
            if forecast_days == 0: return 200, weather_data

        params = {"lat": lat, "lon": lng, "days": forecast_days, "key": key}
        async with session.get(WEATHERBIT_URL, params=params) as response:
            status_code2 = response.status
            response_json2 = await response.json()
        if status_code2 != 200: return status_code2, response_json2

        if weather_data is not None:
            weather_data["time"] += [row["valid_date"] for row in response_json2["data"]]
            weather_data["temp_max"] += [row["max_temp"] for row in response_json2["data"]]
            weather_data["temp_min"] += [row["min_temp"] for row in response_json2["data"]]
            weather_data["weather_code"] += [weatherbit_weather_code[row["weather"]["code"]] for row in response_json2["data"]]
            return 200, weather_data

        else: return 200, WeatherData(
            service="WeatherBit",
            time=[row["valid_date"] for row in response_json2["data"]],
            temp_max=[row["max_temp"] for row in response_json2["data"]],
            temp_min=[row["min_temp"] for row in response_json2["data"]],
            weather_code=[weatherbit_weather_code[row["weather"]["code"]] for row in response_json2["data"]]
        ).__dict__


openweather_weather_code = {
    200: 8, 201: 8, 202: 8, 210: 8, 211: 8, 212: 8, 221: 8, 230: 8, 231: 8, 232: 8, # Thunderstorm
    300: 5, 301: 5, 302: 5, 310: 5, 311: 5, 312: 5, 313: 5, 314: 5, 321: 5, # Drizzle
    500: 6, 501: 6, 502: 6, 503: 6, 504: 6, 511: 6, 520: 6, 521: 6, 522: 6, 531: 6, # Rain
    600: 7, 601: 7, 602: 7, 611: 7, 612: 7, 613: 7, 615: 7, 616: 7, 620: 7, 621: 7, 622: 7, # Snow
    701: 4, 711: 4, 721: 4, 731: 4, 741: 4, 751: 4, 761: 4, 762: 4, 771: 4, 781: 4, # Atmosphere
    800: 0, # Clear sky
    801: 1, 802: 2, 803: 3, 804: 3, # Clouds
}

OPENWEATHER_URL = "https://pro.openweathermap.org/data/2.5/forecast/daily"

async def get_openweather_weather(lat, lng, _, forecast_days, key):
    if forecast_days == 0: return 200, WeatherData("OpenWeatherMap", [], [], [], []).__dict__
    params = {
        "lat": lat, "lon": lng, "cnt": forecast_days, "units": "metric",
        "appid": key,
    }
    async with aiohttp.ClientSession() as session:
        async with session.get(OPENWEATHER_URL, params=params) as response:
            status_code = response.status
            response_json = await response.json()
    if status_code != 200: return status_code, response_json
    else: return 200, WeatherData(
        service="OpenWeatherMap",
        time=[datetime.fromtimestamp(row["dt"]).strftime('%Y-%m-%d') for row in response_json["list"]],
        temp_max=[row["temp"]["max"] for row in response_json["list"]],
        temp_min=[row["temp"]["min"] for row in response_json["list"]],
        weather_code=[openweather_weather_code[row["weather"][0]["id"]] for row in response_json["list"]]
    ).__dict__

def get_avg_weather(data1, data2, data3):
    if data2["service"] == "OpenMeteo": data1, data2 = data2, data1
    elif data3["service"] == "OpenMeteo": data1, data3 = data3, data1
    if data2["service"] == "OpenWeatherMap": data2, data3 = data3, data2
    temp_max, temp_min = [], []
    if len(data1["time"]) == len(data3["time"]):
        temp_max = [round((a+b+c)/3, 1) for a, b, c in zip(data1["temp_max"], data2["temp_max"], data3["temp_max"])]
        temp_min = [round((a+b+c)/3, 1) for a, b, c in zip(data1["temp_min"], data2["temp_min"], data3["temp_min"])]
    else:
        past_days = len(data1["time"]) - len(data3["time"])
        for i in range(past_days):
            temp_max.append(round((data1["temp_max"][i] + data2["temp_max"][i]) / 2, 1))
            temp_min.append(round((data1["temp_min"][i] + data2["temp_min"][i]) / 2, 1))
        for i in range(len(data3["time"])):
            temp_max.append(round((data1["temp_max"][i+past_days] + data2["temp_max"][i+past_days] + data3["temp_max"][i]) / 3, 1))
            temp_min.append(round((data1["temp_min"][i+past_days] + data2["temp_min"][i+past_days] + data3["temp_min"][i]) / 3, 1))

    return 200, WeatherData(
        service="Average",
        time=data1["time"],
        temp_max=temp_max,
        temp_min=temp_min,
        weather_code=data1["weather_code"]
    ).__dict__

def calc_additional_data(data1, data2, data3, avg_data):
    return {
        "type": "accumulated_data",
        data1["service"]: {
            "avg_temp_max": round(sum(data1["temp_max"]) / len(data1["temp_max"]), 2),
            "max_temp_max": round(max(data1["temp_max"]), 2),
            "avg_temp_min": round(sum(data1["temp_min"]) / len(data1["temp_min"]), 2),
            "min_temp_min": round(min(data1["temp_min"]), 2),
            "avg_temp": round(sum(data1["temp_max"] + data1["temp_min"]) / (len(data1["temp_max"]) + len(data1["temp_min"])), 2),
        },
        data2["service"]: {
            "avg_temp_max": round(sum(data2["temp_max"]) / len(data2["temp_max"]), 2),
            "max_temp_max": round(max(data2["temp_max"]), 2),
            "avg_temp_min": round(sum(data2["temp_min"]) / len(data2["temp_min"]), 2),
            "min_temp_min": round(min(data2["temp_min"]), 2),
            "avg_temp": round(sum(data2["temp_max"] + data2["temp_min"]) / (len(data2["temp_max"]) + len(data2["temp_min"])), 2),
        },
        data3["service"]: {
            "avg_temp_max": round(sum(data3["temp_max"]) / len(data3["temp_max"]), 2),
            "max_temp_max": round(max(data3["temp_max"]), 2),
            "avg_temp_min": round(sum(data3["temp_min"]) / len(data3["temp_min"]), 2),
            "min_temp_min": round(min(data3["temp_min"]), 2),
            "avg_temp": round(sum(data3["temp_max"] + data3["temp_min"]) / (len(data3["temp_max"]) + len(data3["temp_min"])), 2),
        },
        "Average": {
            "avg_temp_max": round(sum(avg_data["temp_max"]) / len(avg_data["temp_max"]), 2),
            "max_temp_max": round(max(avg_data["temp_max"]), 2),
            "avg_temp_min": round(sum(avg_data["temp_min"]) / len(avg_data["temp_min"]), 2),
            "min_temp_min": round(min(avg_data["temp_min"]), 2),
            "avg_temp": round(sum(avg_data["temp_max"] + avg_data["temp_min"]) / (len(avg_data["temp_max"]) + len(avg_data["temp_min"])), 2),
        }
    }