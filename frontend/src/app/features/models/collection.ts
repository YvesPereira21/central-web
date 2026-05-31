import { Article } from "./article";
import { Question } from "./question";

export interface Collection {
    collectionId: string;
    name: string;
    articles: Article[];
    questions: Question[];
}

export interface CollectionCreate {
    name: string;
}
