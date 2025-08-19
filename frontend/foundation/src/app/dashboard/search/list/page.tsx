import { NavigationBar } from "@/app/navigation/navigation-bar";
import AuthWrapper from "../../../auth/wrapper";
import SearchesMetadataListServer from "./searches-metadata-list-server";

export default async function Page() {
    return <AuthWrapper>
        <NavigationBar />
        <SearchesMetadataListServer></SearchesMetadataListServer>
    </AuthWrapper>
}