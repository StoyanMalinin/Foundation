"use server";

import {cookies} from "next/headers";
import {SearchesMetadata} from "@/app/dashboard/search/data";
import { FoundationBackend } from "@/backend/foundation-backend";

export async function fetchAdminSearches() {
    const response = await FoundationBackend.fetchAdminSearches((await cookies()).get("jwt")?.value ?? "");
    const data = await response.json();

    return data as SearchesMetadata[];
}

export async function fetchSearches() {
    const response = await FoundationBackend.fetchSearches();
    const data = await response.json();

    return data as SearchesMetadata[];
}