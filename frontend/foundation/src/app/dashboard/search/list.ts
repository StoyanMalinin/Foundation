"use server";

import {cookies} from "next/headers";
import {SearchesMetadata} from "@/app/dashboard/search/data";

export async function fetchAdminSearches() {
    const response = await fetch("https://localhost:6969/admin-searches-metadata", {
        credentials: "include",
        headers: {
            'Authorization': `Bearer ${(await cookies()).get("jwt")?.value}`
        }
    });
    const data = await response.json();

    return data as SearchesMetadata[];
}

export async function fetchSearches() {
    const response = await fetch("https://localhost:6969/searches-metadata");
    const data = await response.json();

    return data as SearchesMetadata[];
}