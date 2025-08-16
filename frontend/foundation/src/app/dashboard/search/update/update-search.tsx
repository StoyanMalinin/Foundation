import { Search } from "../form";
import UpdateSearchForm from "./update-search-form";

export default async function UpdateSearch({ searchParams }) {
    const searchId = (await searchParams).id;

    const res = await fetch(`https://localhost:6969/search?id=${searchId}`);
    const search = await res.json() as Search;

    return (
        <UpdateSearchForm search={search} />
    );
}