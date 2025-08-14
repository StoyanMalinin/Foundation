"use client";

import { useEffect, useState } from "react";
import { JWTManager } from "./auth/jwt/jwt-manager";

export default function Home() {
  const [username, setUsername] = useState<String>();
  useEffect(() => {
    const fetchUsername = async () => {
      try {
        const name = await JWTManager.getUsername();
        setUsername(name);
      } catch (error) {}
    };

    fetchUsername();
  }, []);

  return <p>Hello, {username == "" ? "Guest" : username}</p>;
}
