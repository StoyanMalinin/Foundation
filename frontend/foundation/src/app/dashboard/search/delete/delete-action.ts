"use server";

import {revalidatePath} from "next/cache";
import {cookies} from "next/headers";

export async function deleteAction(searchId: number) {
    const res = await fetch(`https://localhost:6969/delete-search?id=${searchId}`, {
        method: 'DELETE',
        credentials: 'include',
        headers: {
            'Authorization': `Bearer ${(await cookies()).get("jwt")?.value}`
        }
    });

    revalidatePath(".");
    return res.ok;
}