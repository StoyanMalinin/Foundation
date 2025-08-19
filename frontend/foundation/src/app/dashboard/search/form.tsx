"use client";

import { Box, Button, FormControl, FormControlLabel, FormLabel, Input, Paper, TextField } from "@mui/material";
import Form from "next/form";

export type Search = {
    id: number;
    title: string;
    description: string;
};

export type SearchFormState = {
    search: Search;
    errorMessage: string | null;
}

export function SearchForm(
    {formAction, formState, submitText}: {
        formAction: (formData: FormData) => void, formState: SearchFormState, submitText: string}
) {
    const errorText = (<label style={{color: "red"}}>{formState.errorMessage}</label>);
    return (
        <Paper sx={{width: "500px"}}>
            <Form action={formAction} width="100%">
                <FormControl sx={{width: "60%"}}>
                    <FormLabel htmlFor="title">Title</FormLabel>
                    <Input id="title" type="text" placeholder="Title" name="title" defaultValue={formState.search.title} />
                </FormControl><br></br><br></br>
                <FormControl sx={{width: "90%"}}>
                    <FormLabel htmlFor="description">Description</FormLabel>
                    <TextField id="description" multiline variant="outlined" type="text" placeholder="Description" name="description" defaultValue={formState.search.description} />
                </FormControl><br></br><br></br>

                <Box sx={{display: "flex", justifyContent: "center"}}>
                    <Button type="submit" variant="contained">{submitText}</Button>
                </Box>
            </Form>

            {formState.errorMessage != "" && <><br/><br/>{errorText}</>}
        </Paper>
    )
}
