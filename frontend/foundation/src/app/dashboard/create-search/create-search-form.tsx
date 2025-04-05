import Form from 'next/form'

export default async function CreateSearchForm() {
    return (
        <Form action={async function(){"use server";}}></Form>
    );
}
