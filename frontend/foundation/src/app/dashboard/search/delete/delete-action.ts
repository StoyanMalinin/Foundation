"use server";

import {revalidatePath} from "next/cache";
import {cookies} from "next/headers";
import { FoundationBackend } from "@/backend/foundation-backend";

export async function deleteAction(searchId: number) {
    const res = await FoundationBackend.deleteSearch(searchId, (await cookies()).get("jwt")?.value ?? "");

    revalidatePath(".");
    return res.ok;
}