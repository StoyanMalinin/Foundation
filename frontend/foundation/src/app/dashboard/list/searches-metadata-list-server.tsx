import SearchesMetadataListClient from "./searches-metadata-list-client";

export default async function SearchesMetadataListServer() {
    const response = await fetch("https://localhost:6969/searches-metadata");
    const data = await response.json() as SearchesMetadata[];

    return <SearchesMetadataListClient searches={data} />;
}