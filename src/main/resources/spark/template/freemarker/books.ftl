<html>
    <head>
        <title>Books Library</title>
        <link rel="stylesheet" href="/style.css">
    </head>
    <body>
        <header>Books Library</header>

        <#list books as id, book>
            <section>
                <p>Id: ${id}</p>
                <p>Title: ${book.title}</p>
                <p>Author: ${book.author}</p>
                <p>Pages: ${book.pages}</p>
                <a class="delete" href="#">Delete</a>
            </section>
        </#list>

        <footer>Java Programming - Harbour.Space University</footer>
    </body>
</html>