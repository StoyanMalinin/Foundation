import {fetchSearches, fetchAdminSearches} from "../list";
import SearchesMetadataListWithSwitch from "@/app/dashboard/search/list/serches-metadata-list-with-switch";

export default async function SearchesMetadataListServer() {
    const searches = await fetchSearches();
    const adminSearches = await fetchAdminSearches();

    return <SearchesMetadataListWithSwitch searches={searches} adminSearches={adminSearches} />;
}