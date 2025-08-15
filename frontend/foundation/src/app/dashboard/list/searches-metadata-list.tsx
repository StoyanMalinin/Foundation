type SearchesMetadata = {
    title: string;
}

export default async function SearchesMetadataList() {
    const response = await fetch("https://localhost:6969/searches-metadata");
    const data = await response.json() as SearchesMetadata[];

    return (
    <>
        <table>
        <tbody>
            <tr>
                <th>Title</th>
                <th>Actions</th>
            </tr>
            {data.map((item: { title: string; }) => (
                <tr key={item.title}>
                    <td>{item.title}</td>
                    <td>TODO</td>
                </tr>
            ))}
        </tbody>
        </table>
    </>);
}