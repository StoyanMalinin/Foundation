import { Search } from "../form";
import UpdateSearchForm from "./update-search-form";
import { FoundationBackend } from "@/backend/foundation-backend";

// @ts-expect-error: Ignore implicit 'any' type error for 'searchParams'
export default async function UpdateSearch({ searchParams }) {
    const searchId = (await searchParams).id;

    const res = await FoundationBackend.getSearch(searchId);
    const search = await res.json() as Search;

    return (
        <UpdateSearchForm search={search} />
    );
}