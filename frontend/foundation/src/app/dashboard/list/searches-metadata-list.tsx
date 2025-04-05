const https = require('node:https') as typeof import('node:https');
const fetch = require('node-fetch') as typeof import('node-fetch');
const httpsAgent = new https.Agent({rejectUnauthorized: false, keepAlive: true});

type SearchesMetadata = {
    title: string;
}

export default async function SearchesMetadataList() {
    const response = await fetch.default("https://localhost:6969/searches-metadata", {agent: httpsAgent});
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