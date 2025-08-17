import { Search } from "../data";
import ViewSearchClient from "./view-search-client";

export default async function ViewSearchServer({searchId}: {searchId: number}) {
    const res = await fetch(`https://localhost:6969/search?id=${searchId}`);
    const search = await res.json() as Search;

    return <ViewSearchClient search={search} />;
}