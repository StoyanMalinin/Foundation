import {fetchSearches, fetchAdminSearches} from "../list";
import SearchesMetadataListClient from "../searches-metadata-list-client";
import SearchesMetadataListWithSwitch from "@/app/dashboard/search/list/serches-metadata-list-with-switch";

export default async function SearchesMetadataListServer() {
    const searches = await fetchSearches();
    const adminSearches = await fetchAdminSearches();

    return <SearchesMetadataListWithSwitch searches={searches} adminSearches={adminSearches} />;
}