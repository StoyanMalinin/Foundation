"use client";

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
        const response = await fetch("https://localhost:6969/who-am-i", {
          method: "GET",
          credentials: "include",
        });
        const data = await response.json();
        setUserData({
          firstName: data["first_name"],
          lastName: data["last_name"],
        });
      } catch (error) {}
    };

    fetchUserData();
  }, []);

  return <h1>Hello, {userData == null ? "Guest" : `${userData.firstName} ${userData.lastName}`}</h1>;
}
