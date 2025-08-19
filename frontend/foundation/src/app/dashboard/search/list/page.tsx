import AuthWrapper from "../../../auth/wrapper";
import SearchesMetadataListServer from "./searches-metadata-list-server";

export default async function Page() {
    return <AuthWrapper>
        <SearchesMetadataListServer></SearchesMetadataListServer>
    </AuthWrapper>
}