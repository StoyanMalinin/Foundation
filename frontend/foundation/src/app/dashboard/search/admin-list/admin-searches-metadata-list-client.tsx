"use client";

import { Button, Table, TableBody, TableCell, TableHead, TableRow } from "@mui/material";
import { useRouter } from "next/navigation";
import {useState} from "react";
import {DeleteModal} from "@/app/dashboard/search/delete/delete-modal";

type SearchesMetadata = {
    id: number;
    title: string;
}

export default function AdminSearchesMetadataListClient({searches}: {searches: SearchesMetadata[]}) {
    const [deleteModalOpen, setDeleteModalOpen] = useState<boolean>(false);
    const [searchToDelete, setSearchToDelete] = useState<SearchesMetadata | null>(null);

    return <>
        <DeleteModal isOpen={deleteModalOpen} setIsOpen={setDeleteModalOpen}
                     searchId={searchToDelete?.id ?? null} searchTitle={searchToDelete?.title ?? null}>
        </DeleteModal>
        <Table>
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
                            <DeleteButton search={item} setSearchToDelete={setSearchToDelete} setDeleteModalOpen={setDeleteModalOpen} />
                            <MapButton searchId={item.id} />
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    </>
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

function DeleteButton({search, setDeleteModalOpen, setSearchToDelete}: {
    search: SearchesMetadata,
    setDeleteModalOpen: (arg0: boolean) => void,
    setSearchToDelete: (arg0: SearchesMetadata | null) => void
                      }) {

    return <Button variant="contained" onClick={() => {
        setDeleteModalOpen(true);
        setSearchToDelete(search);
    }}>
        Delete
    </Button>
}