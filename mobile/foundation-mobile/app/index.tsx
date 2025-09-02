import { Button } from "@react-navigation/elements";
import { useRouter } from "expo-router";
import { Text, View } from "react-native";
import { AuthWrapper } from "./auth/auth-wrapper";

export default function Index() {
  const router = useRouter();

  return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <AuthWrapper>
        <Text>Welcome to the Foundation!</Text>
        <Button onPress={() => {router.push('/search-selection')}}>Go to searches list</Button>
      </AuthWrapper>
    </View>
  );
}
