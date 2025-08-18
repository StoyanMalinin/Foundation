import {fetchSearches, fetchAdminSearches} from "../list";
import SearchesMetadataListClient from "../searches-metadata-list-client";

export default async function SearchesMetadataListServer() {
    const searches = await fetchSearches();
    const adminSearches = await fetchAdminSearches();

    return <SearchesMetadataListClient searches={searches} adminSearches={adminSearches} />;
}