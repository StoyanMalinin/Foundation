"use client";

import { JWTManager } from "./auth/jwt/jwt-manager";

export default function Home() {
  debugger;
  return <p>Hello, {JWTManager.getUsername()}</p>;
}
