// :)
const weather_emoji = {
    "-1": ["", ""],
    0: ["☀️", "🌙"], 1: ["🌤️", "☁️"], 2: ["⛅", "☁️"],
    3: ["☁️", "☁️"], 4: ["🌫️", "🌫️"], 5: ["🌦️", "🌧️"],
    6: ["🌧️", "🌧️"], 7: ["🌨️", "🌨️"], 8: ["⛈️", "⛈️"],
}

function clearLastWeatherData() {
    Array.from(document.getElementsByClassName("chart_info")).forEach(elem => elem.innerHTML = "");
    Array.from(document.getElementsByClassName("chart")).forEach(elem => elem.innerHTML = "");
    document.getElementById("sum_data").innerHTML = ""
}

//createPlot(JSON.parse(localStorage.getItem("weather_data")));
//localStorage.setItem("weather_data", JSON.stringify(data));

const api_key_header = {"x-api-key": "my_password123"}


function createPlot(data, chartName, serviceName) {

    function formatDateLabel(dateStr) {
        const date = new Date(dateStr);
        const weekDays = ["Nd", "Pn", "Wt", "Śr", "Cz", "Pt", "Sb"];
        return `${weekDays[date.getDay()]}<br>${date.getDate()}.${date.getMonth() + 1}`;
    }

    function createTrace(yData, lineColor, isDay) {
        return {
            x: formattedDates,
            y: yData,
            mode: "lines+markers+text",
            marker: {size: 10, color: "rgb(255,255,255)", line: { color: lineColor, width: 2 }},
            line: { color: lineColor },
            text: yData.map((temp, i) => {
                const code = data.weather_code[i]
                const icon = weather_emoji[code][isDay ? 0 : 1] || "";
                if (icon === "") return `${temp}°C`;
                else return `<span style="font-size: 24px">${icon}</span><br>${temp}°C`;
            }),
            textposition: "top center",
            textfont: { weight: "bold", size: 14, family: "Arial" },
            hoverinfo: "none",
            cliponaxis: false
        };
    }

    const formattedDates = data.time.map(formatDateLabel);

    const traceMax = createTrace(data.temp_max, "orange", true);
    const traceMin = createTrace(data.temp_min, "#85bbcf", false);

    const todayStr = new Date().toISOString().split("T")[0];
    const todayIndex = data.time.indexOf(todayStr);
    const todayXpos = formatDateLabel(data.time[todayIndex]);
    const todayLine = todayIndex !== -1 ? [{
        type: "line",
        x0: todayXpos, x1: todayXpos,
        y0: Math.min(...data.temp_min) - 2,
        y1: Math.max(...data.temp_max) + 2,
        line: { color: "red", width: 2, dash: "dash" },
    }] : [];

    const layout = {
        xaxis: { tickfont: { weight: "bold" } },
        yaxis: { tickfont: { weight: "bold" } },
        showlegend: false,
        paper_bgcolor: "white",
        plot_bgcolor: "transparent",
        margin: { t: 30, b: 40, l: 30, r: 10 },
        shapes: todayLine
    };

    document.getElementById(`${chartName}_info`).innerText = `Prognoza z serwisu ${serviceName}:`
    Plotly.newPlot(chartName, [traceMin, traceMax], layout, {staticPlot: true});
}

function fetchWeatherData(locData, formDataObj) {
    const params = new URLSearchParams({
        lat: locData["latlng"][0],
        lon: locData["latlng"][1],
        past_days: formDataObj.past_days,
        forecast_days: formDataObj.forecast_days,
        api_key: api_key_header["x-api-key"]
    });

    const sum_days = parseInt(formDataObj.past_days) + parseInt(formDataObj.forecast_days);

    clearLastWeatherData();
    document.getElementById("all_charts_info").innerText = `Prognoza dla ${locData["city"]} na ${sum_days} dni`;

    const eventSource = new EventSource("http://127.0.0.1:8000/weather/stream?" + params.toString());

    let index = 1;
    eventSource.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
            console.log(`Data${index}:`, data);
            if (data["type"] === "plot") {
                createPlot(data, "chart" + index, data["service"]);
                index++;
            } else if (data["type"] === "accumulated_data") {
                document.getElementById("sum_data").innerText = JSON.stringify(data, null, 4);
            }
        } catch (err) {
            console.error("Error parsing JSON:", err);
        }

    };

    eventSource.onerror = (error) => {
        console.error("EventSource failed:", error);
        eventSource.close();
    };
}


const formElem = document.getElementById("form");

formElem.addEventListener("submit", (e) => {
    e.preventDefault();
    const formData = new FormData(formElem);
    const formDataObj = Object.fromEntries(formData);
    console.log("Form:", formDataObj);

    fetch("http://localhost:8000/location?city=" + formDataObj.city,
        {method: "GET", headers: api_key_header})
        .then(response => response.json())
        .then(locData => {
            console.log("Location Data:", locData);
            if (!locData) return;
            fetchWeatherData(locData, formDataObj);
        })
        .catch(error => {console.log(error)})
});


