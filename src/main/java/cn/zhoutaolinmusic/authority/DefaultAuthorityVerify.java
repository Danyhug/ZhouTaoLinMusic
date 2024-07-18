package cn.zhoutaolinmusic.authority;

import javax.servlet.http.HttpServletRequest;

public class DefaultAuthorityVerify implements AuthorityVerify {

    @Override
    public Boolean authorityVerify(HttpServletRequest request, String[] permissions) {
        return true;
    }
}
