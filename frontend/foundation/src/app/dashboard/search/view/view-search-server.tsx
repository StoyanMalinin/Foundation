import { Search } from "../data";
import ViewSearchClient from "./view-search-client";
import { FoundationBackend } from "@/backend/foundation-backend";

export default async function ViewSearchServer({searchId}: {searchId: number}) {
    const res = await FoundationBackend.getSearch(searchId);
    const search = await res.json() as Search;

    return <ViewSearchClient search={search} />;
}