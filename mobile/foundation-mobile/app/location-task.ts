import BackgroundService from 'react-native-background-actions';

async function sleepMs(ms: number) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

let locationTaskRunningState = true;
export async function locationTask() {
    console.log("hello");
    while (locationTaskRunningState) {
        await sendLocation();
        await sleepMs(1000);
    }
}

async function sendLocation() {
    console.log("Sending location...");
}

export async function checkLocationTaskRunning() {
    return BackgroundService.isRunning();
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

    locationTaskRunningState = true;
    await BackgroundService.start(locationTask, options);
}

export async function stopLocationTask() {
    locationTaskRunningState = false;
    await BackgroundService.stop();
}