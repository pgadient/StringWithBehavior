# BString: String With Behavior
The BString framework offers a `String` class, which can react depending on the contained value.
In consequence, the behavior of an application will adjust depending on the contained `String` values and their locations.
For this purpose, we introduced two additional methods in the OpenJDK Java `String` class implementation, which accept compiled byte code as an input parameter.
The provided code will be executed before every read, respectively before every write on the object.
For example, if such a `String` object's value is requested from within a `FileWriter` instance, it can either block the request and raise an exception, grant and log the request, or return a safe replacement value that indicates the need for protection.
The use of such strings has only a minor average performance impact on accesses, of less than 16%, is completely optional and, if not used, it does not alter the behavior of existing code.

The framework relies on OpenJDK.

> This is the accompanying material for the book titled “The Dilemma of Security Smells and How to Escape It” that has been published at [lulu.com](https://www.lulu.com/) (ISBN: 978-1-4717-2651-4). The preprint can be downloaded [here](https://boristheses.unibe.ch/view/graduation_year/2022.html).