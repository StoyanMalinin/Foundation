"use client";

import { useActionState } from "react";
import { SearchForm, Search, SearchFormState } from "../form";
import { redirect } from "next/navigation";

export default function CreateSearchForm() {
    const initialSearch: Search = {
        id: 0, // Will be set by the backend
        title: "",
        description: "",
    };

    const [formState, formAction] = useActionState(createSearchAction, {
        search: initialSearch,
        errorMessage: null,
    });

    return (
        <>
            <h1>Create Search</h1>
            <SearchForm formAction={formAction} formState={formState} submitText="Create" />
        </>
    );
}

async function createSearchAction(prevState: SearchFormState, formData: FormData): Promise<SearchFormState> {
    const newSearch: Omit<Search, 'id'> = {
        title: formData.get("title") as string,
        description: formData.get("description") as string,
    };

    const res = await fetch(`https://localhost:6969/create-search`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(newSearch),
        credentials: "include",
    });

    if (res.status != 201) {
        const errorData = await res.text();
        return {
            search: prevState.search,
            errorMessage: errorData,
        };
    }
    
    // Redirect to the search list or another appropriate page on successful creation
    redirect('/dashboard/search/admin-list');
}
