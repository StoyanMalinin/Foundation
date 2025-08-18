"use client";

import {Box, Modal, Typography} from "@mui/material";
import Button from "@mui/material/Button";
import {deleteAction} from "./delete-action";

export function DeleteModal({isOpen, setIsOpen, searchId, searchTitle}: {
    isOpen: boolean,
    setIsOpen: (isOpen: boolean) => void,
    searchId: number | null,
    searchTitle: string | null,
}) {
    if (searchId == null || searchTitle == null) return <p></p>;

    return <Modal open={isOpen} onClose={() => setIsOpen(false)}>
        <Box sx={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            width: 400,
            bgcolor: 'background.peper',
            border: '2px solid #000',
            boxShadow: 12,
            p: 4,
            backgroundColor: 'white',
        }}>
            <Typography display="flex" justifyContent="center">Are you sure you want to delete <b>{searchTitle}</b>?</Typography>
            <br></br>
            <Box display="flex" justifyContent="center" gap="10px">
                <Button variant="contained" onClick={() => setIsOpen(false)}>No</Button>
                <Button variant="contained" onClick={() => deleteSearch(searchId, setIsOpen)}>Yes</Button>
            </Box>
        </Box>
    </Modal>
}

async function deleteSearch(searchId: number, setIsOpen: (isOpen: boolean) => void) {
    const success = await deleteAction(searchId); // call server action
    if (success) {
        setIsOpen(false);
    }
}