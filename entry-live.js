var config = {
    temperature: {
        convertToFahrenheit: true,
    }
}

var temperature = 0;
var lastUpdatedTime = new Date(0);

var temperatureView;
var timeView;


function onClick() {
    var temperatureConfig = config.temperature;
    if (temperatureConfig.convertToFahrenheit === true) {
        temperature = temperature * 1.8 + 32;
    }
    lastUpdatedTime = new Date();
    updateViews(lastUpdatedTime, temperature);
}

function updateViews(t, d) {
    temperatureView = d;
    timeView = t;
}