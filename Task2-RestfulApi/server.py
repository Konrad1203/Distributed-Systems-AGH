# uvicorn server:app --reload
import asyncio
import json

import aiohttp
from fastapi import FastAPI, Depends, HTTPException, Header
from starlette.middleware.cors import CORSMiddleware
from starlette.responses import JSONResponse, StreamingResponse
import weather_data

app = FastAPI()
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_credentials=True, allow_methods=["*"],
                   allow_headers=["*"])

keys = None

required_values = ("geocoder_username", "weatherbit_key", "openweathermap_key", "this_API_password")
file_name = "api_keys.json"
try:
    with open(file_name) as file:
        keys = json.load(file)

    missing_keys = [k for k in required_values if k not in keys]
    if missing_keys:
        raise KeyError(f"Keys file is missing required keys: {', '.join(missing_keys)}")

    empty_keys = [k for k, v in keys.items() if v in (None, "", [])]
    if empty_keys:
        raise ValueError(f"Keys file has empty values for keys: {', '.join(empty_keys)}")

except FileNotFoundError as e:
    print("Keys file not found. Please create", file_name, "file with keys:", ', '.join(required_values), e)
except json.JSONDecodeError as e:
    print("Error during reading keys file:", e)
except TypeError as e:
    print(f"Invalid data structure in '{file_name}': Expected a dictionary.", e)


API_KEY = keys.get("this_API_password")

def verify_api_key(x_api_key: str = Header(None)):
    if x_api_key != API_KEY:
        raise HTTPException(status_code=401, detail="Incorrect API key")

def verify_params(lat, lon, past_days, forecast_days):
    if not (-90 <= lat <= 90):
        raise HTTPException(status_code=400, detail="Incorrect latitude")
    if not (-180 <= lon <= 180):
        raise HTTPException(status_code=400, detail="Incorrect longitude")
    if not (0 <= past_days <= 30):
        raise HTTPException(status_code=400, detail="Incorrect number of past days. Should be between 0 and 30")
    if not (0 <= forecast_days <= 16):
        raise HTTPException(status_code=400, detail="Incorrect number of forecast days. Should be between 0 and 16")


@app.get("/location", dependencies=[Depends(verify_api_key)])
async def location(city: str):
    if not city.isalpha():
        raise HTTPException(status_code=400, detail="City name should contain only letters")
    city_name, latlng = await geocode(city)

    if city_name is None:
        return JSONResponse(status_code=404, content={"error": "City not found"})
    if latlng is None or not isinstance(latlng, tuple) or len(latlng) != 2:
        return JSONResponse(status_code=500, content={"error": "Error during geocoding"})
    return JSONResponse(status_code=200, content={"city": city_name, "latlng": latlng})


@app.get("/weather/stream")
async def weather_stream(lat: float, lon: float, past_days: int, forecast_days: int, api_key: str):
    verify_api_key(api_key)
    verify_params(lat, lon, past_days, forecast_days)
    return StreamingResponse(weather_data_stream(lat, lon, past_days, forecast_days), media_type="text/event-stream")


async def weather_data_stream(lat, lon, past_days, forecast_days):
    results = []
    tasks = [
        asyncio.create_task(weather_data.get_open_meteo_weather(lat, lon, past_days, forecast_days)),
        asyncio.create_task(weather_data.get_weatherbit_weather(lat, lon, past_days, forecast_days, keys.get("weatherbit_key"))),
        asyncio.create_task(weather_data.get_openweather_weather(lat, lon, past_days, forecast_days, keys.get("openweathermap_key")))
    ]
    for task in asyncio.as_completed(tasks):
        try:
            status_code, data = await task
            results.append(data)
            if status_code == 200:
                yield f"data: {json.dumps(data)}\n\n"
        except Exception as e:
            yield f"error: {str(e)}\n\n"
        if len(results) == 3:
            status_code, avg_data = weather_data.get_avg_weather(*results)
            if status_code == 200:
                yield f"data: {json.dumps(avg_data)}\n\n"
                yield f"data: {json.dumps(weather_data.calc_additional_data(*results, avg_data))}\n\n"


@app.get("/weather/open-meteo", dependencies=[Depends(verify_api_key)])
async def get_open_meteo_weather(lat: float, lon: float, past_days: int, forecast_days: int):
    verify_params(lat, lon, past_days, forecast_days)
    status_code, data = await weather_data.get_open_meteo_weather(lat, lon, past_days, forecast_days)
    return JSONResponse(status_code=status_code, content=data)


@app.get("/weather/weatherbit", dependencies=[Depends(verify_api_key)])
async def get_weatherbit_weather(lat: float, lon: float, past_days: int, forecast_days: int):
    verify_params(lat, lon, past_days, forecast_days)
    status_code, data = await weather_data.get_weatherbit_weather(lat, lon, past_days, forecast_days, keys.get("weatherbit_key"))
    return JSONResponse(status_code=status_code, content=data)


@app.get("/weather/openweather", dependencies=[Depends(verify_api_key)])
async def get_openweather_weather(lat: float, lon: float, past_days: int, forecast_days: int):
    verify_params(lat, lon, past_days, forecast_days)
    status_code, data = await weather_data.get_openweather_weather(lat, lon, past_days, forecast_days, keys.get("openweathermap_key"))
    return JSONResponse(status_code=status_code, content=data)


async def geocode(city):
    params = {"name": city, "maxRows": 1, "type": "json", "username": keys.get("geocoder_username")}
    async with aiohttp.ClientSession() as session:
        async with session.get("http://api.geonames.org/search?", params=params) as response:
            data = await response.json()
    return data["geonames"][0]["name"], (data["geonames"][0]["lat"], data["geonames"][0]["lng"])
