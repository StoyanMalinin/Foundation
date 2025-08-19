"use client";

import { useActionState } from "react";
import {SearchForm, Search, SearchFormState} from "../form";
import { Box } from "@mui/material";

export default function UpdateSearchForm({ search }: { search: Search }) {
    const [formState, formAction] = useActionState(updateSearchAction, {
        search: search,
        errorMessage: null,
    });

    return (
        <Box sx={{display: "flex", justifyContent: "center"}}>
            <Box>
                <h1>Update Search</h1>
                <SearchForm formAction={formAction} formState={formState} submitText="Update" />
            </Box>
        </Box>
    );
}

async function updateSearchAction(prevState: SearchFormState, formData: FormData): Promise<SearchFormState> {
    const updatedSearch: Search = {
        id: prevState.search.id,
        title: formData.get("title") as string,
        description: formData.get("description") as string,
    };

    const res = await fetch(`https://localhost:6969/update-search`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(updatedSearch),
        credentials: "include",
    });
    if (res.status != 200) {
        const errorData = await res.text();
        return {
            search: prevState.search,
            errorMessage: errorData,
        };
    }

    return {
        search: updatedSearch,
        errorMessage: null,
    };
}
