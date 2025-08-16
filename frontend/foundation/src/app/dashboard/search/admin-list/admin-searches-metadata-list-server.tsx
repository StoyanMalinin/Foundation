import { cookies } from "next/headers";
import AdminSearchesMetadataListClient from "./admin-searches-metadata-list-client";

export default async function AdminSearchesMetadataListServer() {
    const response = await fetch("https://localhost:6969/admin-searches-metadata", {
        credentials: "include",
        headers: {
            'Authorization': `Bearer ${(await cookies()).get("jwt")?.value}`
        }
    });
    const data = await response.json();

    return <AdminSearchesMetadataListClient searches={data} />;
}