
"use client";

import { useActionState } from "react";
import {SearchForm, Search, SearchFormState} from "../form";
import { FoundationBackend } from "@/backend/foundation-backend";
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

    const res = await FoundationBackend.updateSearch(updatedSearch);
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
