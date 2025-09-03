import BackgroundService from 'react-native-background-actions';

import { FoundationBackend } from '@/backend/foundation-backend';
import * as Location from 'expo-location';
import { getSelectedSearches } from './search-selection/storage';

async function sleepMs(ms: number) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

let locationTaskRunningState = true;
export async function locationTask() {
    while (locationTaskRunningState) {
        await sendLocation();
        await sleepMs(5000);
    }
}

async function sendLocation() {
    const location = await Location.getCurrentPositionAsync({});
    const selectedSearches = await getSelectedSearches();

    const injectResult = await FoundationBackend.injectPresences(selectedSearches, [{lat: location.coords.latitude, lon: location.coords.longitude, recorded_at: Math.trunc(Date.now())}]);

    console.log("Inject result:", injectResult.status);
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