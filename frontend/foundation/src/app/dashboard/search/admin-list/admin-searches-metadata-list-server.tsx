import SearchesMetadataListClient from "../searches-metadata-list-client";
import {fetchAdminSearches} from "../list";

export default async function AdminSearchesMetadataListServer() {
    const data = await fetchAdminSearches();
    return <SearchesMetadataListClient searches={data} adminSearches={data} />;
}