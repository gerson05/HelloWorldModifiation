module Demo
{
    class Response{
        long responseTime;
        string value;
    }

    interface Callback
    {
        void reportResponse(Response r);
    }
    interface Printer
    {
        Response printString(string s);
        void register(Callback* callback);
    }
}