import { Button } from "@react-navigation/elements";
import { useRouter } from "expo-router";
import { useState } from "react";
import { Text, TextInput, View } from "react-native";
import { login } from "../utils";

export default function Index() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  const handleLogin = async function() {
    const loginError = await login(username, password);
    
    if (loginError != null) setError(loginError);
    else router.push("/");
  }

  return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <TextInput
        style = {{
          margin: 5,
          outline: "1px solid black"
        }}
        placeholder="username"
        value={username}
        onChangeText={setUsername}
      />
      <TextInput
        style = {{
          margin: 5,
          outline: "1px solid black"
        }}
        placeholder="password"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
      />
      <Button onPress={handleLogin}>Login</Button>
      {error != null && <Text style={{color: "red"}}>{error}</Text>}
    </View>
  );
}
