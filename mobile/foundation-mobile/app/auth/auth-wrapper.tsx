import { Redirect } from "expo-router";
import React, { ReactNode, useEffect, useState } from "react";
import { Text } from "react-native";
import { isLoggedIn } from "./utils";

export function AuthWrapper({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
  useEffect(() => {
    const checkAuth = async function() {
        const res = await isLoggedIn();
        setIsAuthenticated(res);
    }
    
    checkAuth();
  }, []);


  if (isAuthenticated == null) {
    return <Text>Loading...</Text>;
  }

  if (!isAuthenticated) {
    return <Redirect href="/auth/login" />;
  }
  return <>{children}</>;
}