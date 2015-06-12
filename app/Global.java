import play.*;
import play.mvc.*;
import play.mvc.Http.*;
import play.libs.F.Promise;
import play.libs.F.*;
import static play.mvc.Results.*;

public class Global extends GlobalSettings {

    public Promise<Result> onHandlerNotFound(RequestHeader request) {
        return Promise.<Result>pure(notFound(
            views.html.notFoundPage.render()
        ));
    }
    
    public Promise<Result> onError(RequestHeader request, Throwable t) {
        return Promise.<Result>pure(internalServerError(
            views.html.errorPage.render()
        ));
    }

}