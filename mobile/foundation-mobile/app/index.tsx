import { Button } from "@react-navigation/elements";
import { useRouter } from "expo-router";
import { useEffect, useState } from "react";
import { Text, View } from "react-native";
import { AuthWrapper } from "./auth/auth-wrapper";
import { checkLocationTaskRunning, startLocationTask, stopLocationTask } from "./location-task";

export default function Index() {
  const router = useRouter();
  const [isLocationTaskRunning, setIsLocationTaskRunning] = useState(false);
  useEffect(() => {
    const f = async function() {
      setIsLocationTaskRunning(await checkLocationTaskRunning());
    }

    f();
  }, []);

  const locationTaskState = isLocationTaskRunning ? 
    <Text style={{color: "green", fontSize: 20}}>Location is being tracked</Text> :
    <Text style={{color: "red", fontSize: 20}}>Location is not being tracked</Text>;
  const locationToggle = isLocationTaskRunning ? 
    <Button style={{backgroundColor: "red"}} onPress={() => {stopTracking(setIsLocationTaskRunning)}}>Stop Tracking</Button> :
    <Button style={{backgroundColor: "green"}} onPress={() => {startTracking(setIsLocationTaskRunning)}}>Start Tracking</Button>;

  return (
    <AuthWrapper>
      <View style={{
            flex: 1,
            alignItems: "center",
      }}>

        <View style={{margin: 20}}>
          {locationTaskState}
          {locationToggle}
        </View>

        <View style={{
            flex: 1,
            marginTop: 20,
            alignItems: "center",
            justifyContent: "center",
        }}>
          <Text>Welcome to the Foundation!</Text>
          <Button onPress={() => {router.push('/search-selection')}}>Go to searches list</Button>
        </View>
      </View>
    </AuthWrapper>
  );
}

async function stopTracking(setIsLocationTaskRunning) {
  await stopLocationTask();
  setIsLocationTaskRunning(false);
}

async function startTracking(setIsLocationTaskRunning) {
  await startLocationTask();
  setIsLocationTaskRunning(true);
}
