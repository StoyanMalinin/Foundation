import { Text, View } from "react-native";
import { AuthWrapper } from "./auth/auth-wrapper";

export default function Index() {
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
      </AuthWrapper>
    </View>
  );
}
