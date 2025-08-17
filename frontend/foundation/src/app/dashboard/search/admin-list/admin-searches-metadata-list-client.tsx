"use client";

import { Button, Table, TableBody, TableCell, TableHead, TableRow } from "@mui/material";
import { redirect, useRouter } from "next/navigation";

type SearchesMetadata = {
    id: number;
    title: string;
}

export default function AdminSearchesMetadataListClient({searches}: {searches: SearchesMetadata[]}) {
    return <Table>
        <TableHead>
            <TableRow>
                <TableCell align="center" sx={{borderBottom: '1px solid black'}}><b>Title</b></TableCell>
                <TableCell align="center" sx={{borderBottom: '1px solid black'}}><b>Actions</b></TableCell>
            </TableRow>
        </TableHead>
        <TableBody>
            {searches.map((item) => (
                <TableRow key={item.id}>
                    <TableCell align="center">{item.title}</TableCell>
                    <TableCell 
                        align="center"
                        sx={{
                            display: 'flex',
                            gap: '10px',
                            justifyContent: 'center',
                        }}>
                        <ViewButton searchId={item.id} />
                        <UpdateButton searchId={item.id} />
                        <MapButton searchId={item.id} />
                    </TableCell>
                </TableRow>
            ))}
        </TableBody>
    </Table>
}

function UpdateButton({searchId}: {searchId: number}) {
    const router = useRouter();

    return (
        <Button variant="contained" onClick={() => router.push(`/dashboard/search/update?id=${searchId}`)}>
            Update
        </Button>
    );
}

function ViewButton({searchId}: {searchId: number}) {
    const router = useRouter();

    return (
        <Button variant="contained" onClick={() => router.push(`/dashboard/search/view?id=${searchId}`)}>
            View
        </Button>
    );
}

function MapButton({searchId}: {searchId: number}) {
    const router = useRouter();

    return (
        <Button variant="contained" onClick={() => router.push(`/map?id=${searchId}`)}>
            Map
        </Button>
    );
}