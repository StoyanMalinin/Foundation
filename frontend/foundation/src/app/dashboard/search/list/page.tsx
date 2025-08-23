export const dynamic = "force-dynamic";

import { NavigationBar } from "@/app/navigation/navigation-bar";
import SearchesMetadataListServer from "./searches-metadata-list-server";

export default async function Page() {
    return <>
        <NavigationBar />
        <SearchesMetadataListServer></SearchesMetadataListServer>
    </>
}