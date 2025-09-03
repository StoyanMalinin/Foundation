import BackgroundService from 'react-native-background-actions';

async function sleepMs(ms: number) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function locationTask() {
    console.log("hello");
    while (true) {
        await sendLocation();
        await sleepMs(1000);
    }
}

async function sendLocation() {
    console.log("Sending location...");
}

export async function checkLocationTaskRunning() {
    return false;
}

export async function startLocationTask() {
    const options = {
        taskName: 'Location Tracking',
        taskTitle: 'Location Tracking',
        taskDesc: 'Tracking user location',
        taskIcon: {
            name: 'ic_launcher',
            type: 'mipmap',
        },
        color: '#ff00ff',
        linkingURI: 'yourSchemeHere://chat/jane', // Add this
        parameters: {},
    };

    console.log("start location task");
    await BackgroundService.start(locationTask, options);
    console.log("started location task");
}
