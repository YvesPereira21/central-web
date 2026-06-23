import { ArticleListSimpleCollection } from "./article";
import { QuestionListSimpleCollection } from "./question";

export interface Collection {
    collectionId: string;
    name: string;
    articles: ArticleListSimpleCollection[];
    questions: QuestionListSimpleCollection[];
}

export interface CollectionCreate {
    name: string;
}
