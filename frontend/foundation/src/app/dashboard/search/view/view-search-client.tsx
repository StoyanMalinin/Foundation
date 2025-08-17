"use client";

import { Search } from "../data";

export default function ViewSearchClient({search}: {search: Search}) {
    console.log("sssssss", search);
    return (
        <>
            <h1>{search.title}</h1>
            <h2>Created at {new Date(search.created_at).toDateString()}</h2>
            <p>{search.description}</p>
        </>
    );
}
