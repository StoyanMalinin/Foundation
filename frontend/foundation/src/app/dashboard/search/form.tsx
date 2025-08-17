"use client";

import { Box, Button, FormControl, FormControlLabel, FormLabel, Input, TextField } from "@mui/material";
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
        <>
            <Form action={formAction}>
                <FormControl>
                    <FormLabel htmlFor="title">Title</FormLabel>
                    <Input id="title" type="text" placeholder="Title" name="title" defaultValue={formState.search.title} />
                </FormControl><br></br><br></br>
                <FormControl>
                    <FormLabel htmlFor="description">Description</FormLabel>
                    <TextField id="description" multiline variant="outlined" type="text" placeholder="Description" name="description" defaultValue={formState.search.description} />
                </FormControl><br></br><br></br>

                <Button type="submit" variant="contained">{submitText}</Button>
            </Form>

            {formState.errorMessage != "" && <><br/><br/>{errorText}</>}
        </>
    )
}
