"use client";

import { Box, Button, Link } from "@mui/material";
import { FoundationBackend } from "@/backend/foundation-backend";
import { useEffect, useState } from "react";

type UserData = {
  firstName: string;
  lastName: string;
};

export default function Home() {
  const [userData, setUserData] = useState<UserData | null>(null);
  useEffect(() => {
    const fetchUserData = async () => {
      try {
  const response = await FoundationBackend.whoAmI();
        const data = await response.json();
        setUserData({
          firstName: data["first_name"],
          lastName: data["last_name"],
        });
      } catch (error) {}
    };

    fetchUserData();
  }, []);

  const redirect = userData != null ? 
    <Link href="/dashboard/search">
      <Button variant="contained">Go to Dashboard</Button>
    </Link> : 
    <Link href="/auth/login">
      <Button variant="contained">Login</Button>
    </Link>;

  return <Box>
    <h1>Hello, {userData == null ? "Guest" : `${userData.firstName} ${userData.lastName}`}</h1>
    {redirect}
  </Box>;
}
