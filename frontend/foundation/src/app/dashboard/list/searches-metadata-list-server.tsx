import { cookies } from "next/headers";
import SearchesMetadataListClient from "./searches-metadata-list-client";

export default async function SearchesMetadataListServer() {
    const response = await fetch("https://localhost:6969/searches-metadata", {
        credentials: "include",
        headers: {
            'Authorization': `Bearer ${(await cookies()).get("jwt")?.value}`
        }
    });
    console.log("Response:", response);
    const data = await response.json() as SearchesMetadata[];

    return <SearchesMetadataListClient searches={data} />;
}