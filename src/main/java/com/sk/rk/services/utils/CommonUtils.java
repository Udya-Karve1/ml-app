package com.sk.rk.services.utils;

import com.sk.rk.services.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class have some commonly used method.
 * @author uday.karve
 */
@Slf4j
public class CommonUtils {

    /**
     *
     */
    private CommonUtils() {}

    /**
     * Get user-name from current HttpRequest
     * @return
     */
    public static String getInvokingUser() {
        try {
        	return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        			.getAttribute(Constants.HEADER_USER_NAME).toString().trim().toLowerCase();
        } catch (Exception e) {
            return Constants.DEFAULT_INVOKING_USER;

        }
    }

    public static HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    public static String replace(String psWord, String psReplace, String psNewSeg) {
        StringBuilder lsNewStr = new StringBuilder();
        int liFound = 0;
        int liLastPointer = 0;
        do {
            liFound = psWord.indexOf(psReplace, liLastPointer);
            if (liFound < 0) {
                lsNewStr.append(psWord.substring(liLastPointer, psWord.length()));
            } else {
                if (liFound > liLastPointer) {
                    lsNewStr.append(psWord.substring(liLastPointer, liFound));
                }
                lsNewStr.append(psNewSeg);
                liLastPointer = liFound + psReplace.length();
            }
        } while (liFound > -1);
        return lsNewStr.toString();
    }


    /**
     * Prepare paged response from the list.
     * Response contains attributes related to pag.
     * pageNo, pageSize, totalCount and real list.
     *
     * @param projectionList
     * @param records
     * @param pageNo
     * @param pageSize
     * @return
     * @throws BaseException
     */
    public static Map<String, Object> prepareResponseObjectForPagination(List<?> projectionList,
                               List<?> records, Integer pageNo, Integer pageSize) throws BaseException {
        Map<String, Object> response = new HashMap<>();

        try {
            if(records.isEmpty()) {
                response.put("totalRecords", 0);
            } else {
                Method itemCount = projectionList.get(0).getClass().getMethod("getItemCount");
                Object count = itemCount.invoke(projectionList.get(0));
                response.put("totalRecords", count.toString());
            }
        } catch (NoSuchMethodException e) {
            throw new BaseException(400, "Method getItem_Count not found.", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BaseException(400, e.getMessage(), e);
        }

        response.put("data", records);
        response.put("currentPage", pageNo);
        response.put("pageSize", pageSize);

        return response;
    }

}
