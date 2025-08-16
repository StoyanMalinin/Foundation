import { Button, Input } from "@mui/material";
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
                <Input type="text" placeholder="Title" name="title" defaultValue={formState.search.title} /><br></br>
                <Input type="text" placeholder="Description" name="description" defaultValue={formState.search.description} /><br></br>

                <Button type="submit">{submitText}</Button>
            </Form>

            {formState.errorMessage != "" && <><br/><br/>{errorText}</>}
        </>
    )
}
